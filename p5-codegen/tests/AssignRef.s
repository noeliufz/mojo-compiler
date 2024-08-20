	.data
	.balign 8
AssignRef.g:
	.zero 8
	.text
AssignRef:
	pushq %rbp
	movq %rsp,%rbp
L.0:
	leaq AssignRef.g(%rip),%rax
	movq 0(%rax),%rax
#	movq %rax,%rax
#	returnSink
	popq %rbp
	ret
