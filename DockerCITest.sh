#!/bin/sh

set -eu

echo "Building Docker image..."
docker build -t group-e-server-test-image .

echo "Starting Docker container..."
# Remove any existing container with the same name just in case
docker rm -f group-e-server-test-container >/dev/null 2>&1 || true

CONTAINER_ID=$(docker run -d -p 8080:8080 --name group-e-server-test-container group-e-server-test-image)

cleanup() {
	echo "Cleaning up Docker container..."
	docker stop "$CONTAINER_ID" >/dev/null 2>&1 || true
	docker rm "$CONTAINER_ID" >/dev/null 2>&1 || true
}

trap cleanup EXIT INT TERM

REQUEST_PAYLOAD='{"state":null,"options":[{"type":"end_phase","phase":"BUY"}],"reason":null,"player_uuid":"b82e3039-a809-40c0-9669-267fdd62b627"}'

echo "Waiting for server to become ready..."
ATTEMPT=1
while [ "$ATTEMPT" -le 60 ]; do
	if curl -sS -o /dev/null \
		-X POST http://localhost:8080/decide \
		-H "Content-Type: application/json" \
		-d "$REQUEST_PAYLOAD" \
		--fail; then
		break
	fi

	# Check if container is still running
	if [ -z "$(docker ps -q -f id="$CONTAINER_ID")" ]; then
		echo "Container exited early. Logs:" >&2
		docker logs "$CONTAINER_ID" >&2
		exit 1
	fi

	ATTEMPT=$((ATTEMPT + 1))
	sleep 1
done

if [ "$ATTEMPT" -gt 60 ]; then
	echo "Server did not become ready within 60 seconds. Logs:" >&2
	docker logs "$CONTAINER_ID" >&2
	exit 1
fi

echo "Server is ready. Testing response..."
ACTUAL_RESPONSE=$(curl -sS \
	-X POST http://localhost:8080/decide \
	-H "Content-Type: application/json" \
	-d "$REQUEST_PAYLOAD")

EXPECTED_RESPONSE=$(cat server-test-utils/response.json)

NORMALIZED_ACTUAL=$(printf '%s' "$ACTUAL_RESPONSE" | tr -d '[:space:]')
NORMALIZED_EXPECTED=$(printf '%s' "$EXPECTED_RESPONSE" | tr -d '[:space:]')
WRAPPED_EXPECTED=$(printf '{"decision":%s}' "$NORMALIZED_EXPECTED")

if [ "$NORMALIZED_ACTUAL" != "$NORMALIZED_EXPECTED" ] && [ "$NORMALIZED_ACTUAL" != "$WRAPPED_EXPECTED" ]; then
	echo "Server response did not match expected output." >&2
	echo "Expected: $EXPECTED_RESPONSE" >&2
	echo "Actual:   $ACTUAL_RESPONSE" >&2
	exit 1
fi

echo "Docker CI test passed: response matches server-test-utils/response.json"
