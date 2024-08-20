	.text
FixedArrayInit:
	pushq %rbp
	movq %rsp,%rbp
	subq $336,%rsp
	movq %rbx, -328(%rbp)
L.4:
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
	movq $10,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	movq %rax,%rcx
	cmpq %rdx,%rcx
	jge L.3
L.2:
	movq %rcx,%rbx
	imulq $24,%rbx
	movq %rbp,%rax
	addq %rbx,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	movq %rax,-320(%rbx)
	xorq %rax,%rax
	movq %rax,-312(%rbx)
	xorq %rax,%rax
	movq %rax,-304(%rbx)
	movq %rcx,%rax
	addq $1,%rax
	movq %rax,%rcx
	cmpq %rdx,%rcx
	jl L.2
L.3:
#	returnSink
	movq -328(%rbp),%rbx
	addq $336,%rsp
	popq %rbp
	ret
