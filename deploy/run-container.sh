#!/usr/bin/env bash
set -euo pipefail

image=${1:?Usage: run-container.sh IMAGE [ENV_FILE]}
env_file=${2:-/home/roselle/challenge/shared/app.env}
name=challenge-api
previous=challenge-api-previous

test -f "$env_file"
docker pull "$image"
if docker container inspect "$previous" >/dev/null 2>&1; then
    docker rm "$previous"
fi
if docker container inspect "$name" >/dev/null 2>&1; then
    docker stop "$name"
    docker rename "$name" "$previous"
fi

rollback() {
    docker rm -f "$name" >/dev/null 2>&1 || true
    if docker container inspect "$previous" >/dev/null 2>&1; then
        docker rename "$previous" "$name"
        docker start "$name"
    fi
}
trap rollback ERR
docker run -d --name "$name" --restart unless-stopped --network host \
    --log-opt max-size=20m --log-opt max-file=3 \
    --env-file "$env_file" "$image"

ready=false
for attempt in {1..60}; do
    if curl --fail --silent --max-time 3 http://127.0.0.1:8180/api/common/category | \
        python3 -c 'import json,sys; d=json.load(sys.stdin); sys.exit(0 if d.get("code")==10000 and len(d.get("data",[]))==11 else 1)' 2>/dev/null; then
        ready=true
        break
    fi
    sleep 1
done
test "$ready" = true
trap - ERR
docker inspect --format '{{.Name}} {{.State.Status}} {{.Config.Image}}' "$name"
