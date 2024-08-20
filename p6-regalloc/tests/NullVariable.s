	.data
	.balign 8
NullVariable.v:
	.zero 8
	.text
NullVariable:
	pushq %rbp
	movq %rsp,%rbp
L.0:
#	returnSink
	popq %rbp
	ret
