frontend:
	clj -A:frontend

backend-dev:
	RESOURCE_PATH=http://localhost:5444 clj -A:backend -A:nREPL 

test-clj:
	clj -A:test
