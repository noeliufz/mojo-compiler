	.text
Until:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.5:
	xorq %rax,%rax
	movq $1,%rbx
L.0:
	movq $16,%rcx
	cmpq %rcx,%rax
	jge L.2
L.1:
	movq %rax,t.1
	addq $1,t.1
	movq t.1,%rax
	movq %rbx,t.2
	addq %rbx,t.2
	movq t.2,%rbx
	movq $42,%rcx
	cmpq %rcx,%rbx
	je L.3
L.4:
	movq $512,%rcx
	cmpq %rcx,%rbx
	je L.2
L.6:
	jmp L.0
L.3:
L.2:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
Until.badSub:
	call badSub
