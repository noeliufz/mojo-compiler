	.text
BreakStmt:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.3:
	xorq %rax,%rax
	movq %rax,%rcx
	xorq %rax,%rax
	movq %rax,%rbx
L.0:
	xorq %rax,%rax
	cmpq %rax,%rcx
	je L.2
L.1:
	jmp L.2
L.4:
	xorq %rax,%rax
	cmpq %rax,%rbx
	je L.0
L.2:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
