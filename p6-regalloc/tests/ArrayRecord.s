	.text
ArrayRecord:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.2:
	movq $160,%rdi
	call malloc
#	movq %rax,%rax
	movq $10,%rsi
	xorq %rcx,%rcx
	cmpq %rsi,%rcx
	jge L.1
L.0:
	movq %rcx,%rbx
	shlq $4,%rbx
	movq %rax,%rdx
	addq %rbx,%rdx
	movq %rdx,t.3
	movq $0,0(t.3)
	movq $0,8(t.3)
	movq %rcx,t.6
	addq $1,t.6
	movq t.6,%rcx
	cmpq %rsi,%rcx
	jl L.0
L.1:
	movq %rax,ArrayRecord.1.a
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
