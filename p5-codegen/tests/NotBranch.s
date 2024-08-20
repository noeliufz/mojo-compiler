	.text
NotBranch.Not:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rbx
L.3:
	xorq %rax,%rax
	cmpq %rax,%rbx
	je L.1
L.2:
	xorq %rax,%rax
#	movq %rax,%rax
	jmp L.0
L.1:
	movq $1,%rax
#	movq %rax,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
	.text
NotBranch:
	pushq %rbp
	movq %rsp,%rbp
L.4:
#	returnSink
	popq %rbp
	ret
