	.text
LargeParameter.foo:
	pushq %rbp
	movq %rsp,%rbp
	movq %r9,56(%rbp)
	movq %r8,48(%rbp)
	movq %rcx,40(%rbp)
	movq %rdx,32(%rbp)
	movq %rsi,24(%rbp)
	movq %rdi,16(%rbp)
L.0:
#	returnSink
	popq %rbp
	ret
	.text
LargeParameter:
	pushq %rbp
	movq %rsp,%rbp
L.1:
#	returnSink
	popq %rbp
	ret
