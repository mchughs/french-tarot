frontend+backend:
	clojure -m figwheel.main \
	--build dev \
	--repl

backend:
	clj -A:nREPL -m nrepl.cmdline \
	--interactive \
	--color

test-clj:
	clj -A:test
