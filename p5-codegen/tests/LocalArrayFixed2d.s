	.text
LocalArrayFixed2d:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.4:
	movq $400,%rax
	movq %rax,%rdi
	call malloc
	movq %rax,%rbx
	movq $10,%rax
#	movq %rax,%rax
	xorq %rcx,%rcx
	movq %rcx,%r8
	cmpq %rax,%r8
	jge L.1
L.0:
	movq $5,%rcx
	movq %rcx,%rdi
	xorq %rcx,%rcx
	movq %rcx,%rsi
	cmpq %rdi,%rsi
	jge L.3
L.2:
	movq %r8,%rcx
	imulq $40,%rcx
	movq %rbx,%rdx
	addq %rcx,%rdx
	movq %rsi,%rcx
	imulq $8,%rcx
#	movq %rdx,%rdx
	addq %rcx,%rdx
	xorq %rcx,%rcx
	movq %rcx,0(%rdx)
	movq %rsi,%rcx
	addq $1,%rcx
	movq %rcx,%rsi
	cmpq %rdi,%rsi
	jl L.2
L.3:
	movq %r8,%rcx
	addq $1,%rcx
	movq %rcx,%r8
	cmpq %rax,%r8
	jl L.0
L.1:
	movq %rbx,%rax
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
