	.text
LoopWhileCond:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.0:
	movq $1,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je L.2
L.1:
	jmp L.0
L.2:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
