# Geoshop demo

## Requirements

Make sure your ports 8080 and 443 are available.

## Geoshop
Run geoshop

```shell
git clone --recurse-submodules https://github.com/sitn/geoshop-demo.git
cp .env.sample .env
docker compose up -d
```

App is accessible at https://localhost


## Extract

In another terminal

```shell
cd extract
docker compose up -d
```

Extract is available at http://localhost:8080/extract/setup
