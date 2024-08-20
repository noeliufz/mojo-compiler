	.text
NotExpr.Not:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rcx
L.3:
	xorq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je L.1
L.2:
	movq %rbx,%rax
	jmp L.0
L.1:
	movq $1,%rax
	movq %rax,%rbx
	jmp L.2
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
	.text
NotExpr:
	pushq %rbp
	movq %rsp,%rbp
L.4:
#	returnSink
	popq %rbp
	ret
