#!/bin/sh

set -eu

mvn -B package --file automation/pom.xml

java -jar automation/server/target/server-1.0-SNAPSHOT.jar >/tmp/server-ci.log 2>&1 &
SERVER_PID=$!

cleanup() {
	kill "$SERVER_PID" >/dev/null 2>&1 || true
	wait "$SERVER_PID" >/dev/null 2>&1 || true
}

trap cleanup EXIT INT TERM

REQUEST_PAYLOAD='{"state":null,"options":[{"type":"end_phase","phase":"BUY"}],"reason":null,"player_uuid":"b82e3039-a809-40c0-9669-267fdd62b627"}'

ATTEMPT=1
while [ "$ATTEMPT" -le 60 ]; do
	if curl -sS -o /dev/null \
		-X POST http://localhost:8080/decide \
		-H "Content-Type: application/json" \
		-d "$REQUEST_PAYLOAD"; then
		break
	fi

	if ! kill -0 "$SERVER_PID" >/dev/null 2>&1; then
		echo "Server process exited early. Logs:" >&2
		cat /tmp/server-ci.log >&2
		exit 1
	fi

	ATTEMPT=$((ATTEMPT + 1))
	sleep 1
done

if [ "$ATTEMPT" -gt 60 ]; then
	echo "Server did not become ready within 60 seconds. Logs:" >&2
	cat /tmp/server-ci.log >&2
	exit 1
fi

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

echo "Server CI test passed: response matches server-test-utils/response.json"
