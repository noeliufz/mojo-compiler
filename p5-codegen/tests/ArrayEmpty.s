	.data
	.balign 8
ArrayEmpty.v:
	.zero 0
	.text
ArrayEmpty:
	pushq %rbp
	movq %rsp,%rbp
L.0:
#	returnSink
	popq %rbp
	ret
