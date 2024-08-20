	.text
While:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.5:
	xorq %rax,%rax
	movq %rax,%rbx
L.0:
	movq $100,%rax
	cmpq %rax,%rbx
	jge L.2
L.1:
	movq %rbx,%rax
	addq $1,%rax
	movq %rax,%rbx
	movq $42,%rax
	cmpq %rax,%rbx
	je L.3
L.4:
	jmp L.0
L.3:
L.2:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
While.badSub:
	call badSub
