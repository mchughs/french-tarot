frontend:
	clj -A:frontend

backend:
	clj -A:backend -A:nREPL

test-clj:
	clj -A:test
