	.text
LocalArrayFixed:
	pushq %rbp
	movq %rsp,%rbp
	subq $96,%rsp
	movq %rbx, -88(%rbp)
L.2:
	movq $10,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	movq %rax,%rcx
	cmpq %rdx,%rcx
	jge L.1
L.0:
	movq %rcx,%rax
	imulq $8,%rax
	movq %rbp,%rbx
	addq %rax,%rbx
	xorq %rax,%rax
	movq %rax,-80(%rbx)
	movq %rcx,%rax
	addq $1,%rax
	movq %rax,%rcx
	cmpq %rdx,%rcx
	jl L.0
L.1:
#	returnSink
	movq -88(%rbp),%rbx
	addq $96,%rsp
	popq %rbp
	ret
