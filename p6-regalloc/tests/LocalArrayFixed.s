	.text
LocalArrayFixed:
	pushq %rbp
	movq %rsp,%rbp
	subq $96,%rsp
	movq %rbx, -88(%rbp)
L.2:
	movq $10,%rbx
	xorq %rax,%rax
	cmpq %rbx,%rax
	jge L.1
L.0:
	movq $0,-80(%rbp,%rax,8)
	movq %rax,t.2
	addq $1,t.2
	movq t.2,%rax
	cmpq %rbx,%rax
	jl L.0
L.1:
#	returnSink
	movq -88(%rbp),%rbx
	addq $96,%rsp
	popq %rbp
	ret
