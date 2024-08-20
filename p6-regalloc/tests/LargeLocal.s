	.text
LargeLocal:
	pushq %rbp
	movq %rsp,%rbp
	subq $-2147483648,%rsp
	movq %rbx, -2147483648(%rbp)
L.2:
	movq $268435455,%rbx
	xorq %rax,%rax
	cmpq %rbx,%rax
	jge L.1
L.0:
	movq $0,-2147483640(%rbp,%rax,8)
	movq %rax,t.2
	addq $1,t.2
	movq t.2,%rax
	cmpq %rbx,%rax
	jl L.0
L.1:
#	returnSink
	movq -2147483648(%rbp),%rbx
	addq $-2147483648,%rsp
	popq %rbp
	ret
