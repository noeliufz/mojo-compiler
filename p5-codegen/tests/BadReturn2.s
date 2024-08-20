	.text
BadReturn2.foo:
	pushq %rbp
	movq %rsp,%rbp
L.0:
#	returnSink
	popq %rbp
	ret
	.text
BadReturn2:
	pushq %rbp
	movq %rsp,%rbp
L.1:
#	returnSink
	popq %rbp
	ret
