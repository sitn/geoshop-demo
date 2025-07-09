import collections
import json
import os
import sys
from os import path
import psycopg2

Match = collections.namedtuple('Match', ['canton', 'area'])

src = sys.argv[1]
out = sys.argv[2]

perimeter = None
with open(path.join(src, "request.json")) as f:
    srcData = json.loads(f.read())
    perimeter = srcData.get('perimeter')

if not perimeter:
    print("Error: no perimeter defined")
    sys.exit(-1)

conn = psycopg2.connect(database=os.environ["GEODATA_POSTGRES_DB"],
                        host=os.environ["GEODATA_POSTGRES_HOST"],
                        user=os.environ["GEODATA_POSTGRES_USER"],
                        password=os.environ["GEODATA_POSTGRES_PASSWORD"])
cur = conn.cursor()
cur.execute("""
            WITH t AS (
              SELECT
                TRIM(name) AS name,
                ST_AsText(ST_Intersection(
                  geom,
                  ST_Transform(ST_GeomFromText(%s, 4326), 2056)
                )) AS part
              FROM public.cantons
            )
            SELECT t.name, t.part FROM t
            WHERE NOT ST_IsEmpty(part)""",
(perimeter, ))
matches = list(Match(x[0], x[1]) for x in cur.fetchall())
conn.close()

with open(path.join(out, "output.json"), "w+") as f:
    f.write(json.dumps({'matches': matches}))
