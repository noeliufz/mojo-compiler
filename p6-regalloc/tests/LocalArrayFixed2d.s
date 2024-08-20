	.text
LocalArrayFixed2d:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.4:
	movq $400,%rdi
	call malloc
#	movq %rax,%rax
	movq $10,%r8
	xorq %rbx,%rbx
	cmpq %r8,%rbx
	jge L.1
L.0:
	movq $5,%rdi
	xorq %rsi,%rsi
	cmpq %rdi,%rsi
	jge L.3
L.2:
	movq %rbx,%rcx
	imulq $40,%rcx
	movq %rax,%rdx
	addq %rcx,%rdx
	movq $0,0(%rdx,%rsi,8)
	movq %rsi,t.7
	addq $1,t.7
	movq t.7,%rsi
	cmpq %rdi,%rsi
	jl L.2
L.3:
	movq %rbx,t.8
	addq $1,t.8
	movq t.8,%rbx
	cmpq %r8,%rbx
	jl L.0
L.1:
	movq %rax,LocalArrayFixed2d.1.x
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
