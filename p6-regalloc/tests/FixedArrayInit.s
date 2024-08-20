	.text
FixedArrayInit:
	pushq %rbp
	movq %rsp,%rbp
	subq $336,%rsp
	movq %rbx, -328(%rbp)
L.4:
	movq $10,%rbx
	xorq %rax,%rax
	cmpq %rbx,%rax
	jge L.1
L.0:
	movq $0,-80(%rbp,%rax,8)
	movq %rax,t.5
	addq $1,t.5
	movq t.5,%rax
	cmpq %rbx,%rax
	jl L.0
L.1:
	movq $10,%rdx
	xorq %rbx,%rbx
	cmpq %rdx,%rbx
	jge L.3
L.2:
	movq %rbx,%rax
	imulq $24,%rax
	movq %rbp,%rcx
	addq %rax,%rcx
	movq %rcx,t.4
	movq $0,-320(t.4)
	movq $0,-312(t.4)
	movq $0,-304(t.4)
	movq %rbx,t.8
	addq $1,t.8
	movq t.8,%rbx
	cmpq %rdx,%rbx
	jl L.2
L.3:
#	returnSink
	movq -328(%rbp),%rbx
	addq $336,%rsp
	popq %rbp
	ret
