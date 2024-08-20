	.text
AndBranch.And:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rsi,%rcx
	movq %rdi,%rbx
L.4:
	xorq %rax,%rax
	cmpq %rax,%rbx
	je L.2
L.3:
	xorq %rax,%rax
	cmpq %rax,%rcx
	je L.2
L.1:
	xorq %rax,%rax
#	movq %rax,%rax
	jmp L.0
L.2:
	movq $1,%rax
#	movq %rax,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
	.text
AndBranch:
	pushq %rbp
	movq %rsp,%rbp
L.5:
#	returnSink
	popq %rbp
	ret
