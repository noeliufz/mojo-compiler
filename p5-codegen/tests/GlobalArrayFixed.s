	.data
	.balign 8
GlobalArrayFixed.x:
	.zero 80
	.text
GlobalArrayFixed:
	pushq %rbp
	movq %rsp,%rbp
L.0:
#	returnSink
	popq %rbp
	ret
