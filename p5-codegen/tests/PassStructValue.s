	.data
	.balign 8
PassStructValue.r:
	.zero 48
	.text
PassStructValue.callee:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %r9,56(%rbp)
	movq %r8,48(%rbp)
	movq %rcx,40(%rbp)
	movq %rdx,32(%rbp)
	movq %rsi,24(%rbp)
	movq %rdi,16(%rbp)
L.2:
	movq 16(%rbp),%rax
	movq 24(%rbp),%rbx
#	movq %rax,%rax
	addq %rbx,%rax
	movq 32(%rbp),%rbx
#	movq %rax,%rax
	addq %rbx,%rax
	movq 40(%rbp),%rbx
#	movq %rax,%rax
	addq %rbx,%rax
	movq 48(%rbp),%rbx
#	movq %rax,%rax
	addq %rbx,%rax
	movq 56(%rbp),%rbx
#	movq %rax,%rax
	addq %rbx,%rax
#	movq %rax,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
	.text
PassStructValue.caller:
	pushq %rbp
	movq %rsp,%rbp
	subq $64,%rsp
	movq %rbx, -8(%rbp)
L.3:
	leaq PassStructValue.r(%rip),%rbx
	movq 0(%rbx),%rax
	movq %rax,%rdi
	movq 8(%rbx),%rax
	movq %rax,%rsi
	movq 16(%rbx),%rax
	movq %rax,%rdx
	movq 24(%rbx),%rax
	movq %rax,%rcx
	movq 32(%rbx),%rax
	movq %rax,%r8
	movq 40(%rbx),%rax
	movq %rax,%r9
	call PassStructValue.callee
#	movq %rax,%rax
L.1:
#	returnSink
	movq -8(%rbp),%rbx
	addq $64,%rsp
	popq %rbp
	ret
	.text
PassStructValue:
	pushq %rbp
	movq %rsp,%rbp
L.4:
#	returnSink
	popq %rbp
	ret
