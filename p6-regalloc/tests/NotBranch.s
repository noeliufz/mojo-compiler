	.text
NotBranch.Not:
	pushq %rbp
	movq %rsp,%rbp
	movq %rdi,NotBranch.Not.x
L.3:
	xorq %rax,%rax
	cmpq %rax,NotBranch.Not.x
	je L.1
L.2:
	xorq %rax,%rax
	jmp L.0
L.1:
	movq $1,%rax
L.0:
#	returnSink
	popq %rbp
	ret
NotBranch.Not.badSub:
	call badSub
	.text
NotBranch:
	pushq %rbp
	movq %rsp,%rbp
L.4:
#	returnSink
	popq %rbp
	ret
