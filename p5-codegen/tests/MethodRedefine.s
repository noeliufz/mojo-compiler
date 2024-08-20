	.data
	.balign 8
MethodRedefine.A:
	.quad badPtr
	.data
	.balign 8
MethodRedefine.B:
	.quad badPtr
	.quad badPtr
	.text
MethodRedefine:
	pushq %rbp
	movq %rsp,%rbp
L.0:
#	returnSink
	popq %rbp
	ret
