	.data
	.balign 8
Override.U:
	.quad Override.FooU
	.quad badPtr
	.data
	.balign 8
Override.T:
	.quad Override.FooT
	.quad badPtr
	.quad badPtr
	.text
Override:
	pushq %rbp
	movq %rsp,%rbp
L.0:
#	returnSink
	popq %rbp
	ret
