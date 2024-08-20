	.data
	.balign 8
ObjectFields.A:
	.data
	.balign 8
ObjectFields.B:
	.data
	.balign 8
ObjectFields.C:
	.text
ObjectFields:
	pushq %rbp
	movq %rsp,%rbp
L.0:
#	returnSink
	popq %rbp
	ret
