	.text
If:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.6:
	xorq %rbx,%rbx
	movq $1,%rax
	cmpq %rax,%rbx
	je L.0
L.1:
	movq $4,%rax
	cmpq %rax,%rbx
	jl L.3
L.4:
	movq $30,%rbx
L.5:
	jmp L.2
L.0:
	movq $10,%rbx
	jmp L.2
L.3:
	movq $20,%rbx
	jmp L.5
L.2:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
If.badSub:
	call badSub
