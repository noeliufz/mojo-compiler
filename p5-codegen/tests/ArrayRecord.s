	.text
ArrayRecord:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.2:
	movq $160,%rax
	movq %rax,%rdi
	call malloc
	movq %rax,%rsi
	movq $10,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	movq %rax,%rcx
	cmpq %rdx,%rcx
	jge L.1
L.0:
	movq %rcx,%rbx
	imulq $16,%rbx
	movq %rsi,%rax
	addq %rbx,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	movq %rax,0(%rbx)
	xorq %rax,%rax
	movq %rax,8(%rbx)
	movq %rcx,%rax
	addq $1,%rax
	movq %rax,%rcx
	cmpq %rdx,%rcx
	jl L.0
L.1:
	movq %rsi,%rax
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
