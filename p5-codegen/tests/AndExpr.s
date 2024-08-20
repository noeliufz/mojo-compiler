	.text
AndExpr.And:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rsi,%rdx
	movq %rdi,%rcx
L.4:
	xorq %rax,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rcx
	je L.2
L.3:
	xorq %rax,%rax
	cmpq %rax,%rdx
	je L.2
L.1:
	movq $1,%rax
	movq %rax,%rbx
L.2:
	movq %rbx,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
	.text
AndExpr:
	pushq %rbp
	movq %rsp,%rbp
L.5:
#	returnSink
	popq %rbp
	ret
