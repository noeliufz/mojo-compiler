	.text
AssignRef2:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.0:
	movq $8,%rax
	movq %rax,%rdi
	call malloc
	movq %rax,%rbx
	xorq %rax,%rax
	movq %rax,0(%rbx)
	movq %rbx,%rax
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
