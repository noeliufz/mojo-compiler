	.text
Loop:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.5:
	xorq %rax,%rax
L.0:
	movq $100,%rbx
	cmpq %rbx,%rax
	jge L.2
L.1:
	movq %rax,t.1
	addq $1,t.1
	movq t.1,%rax
	movq $42,%rbx
	cmpq %rbx,%rax
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
Loop.badSub:
	call badSub
