	.text
Until:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.5:
	xorq %rax,%rax
	movq %rax,%rcx
	movq $1,%rax
	movq %rax,%rbx
L.0:
	movq $16,%rax
	cmpq %rax,%rcx
	jge L.2
L.1:
	movq %rcx,%rax
	addq $1,%rax
	movq %rax,%rcx
	movq %rbx,%rax
	addq %rbx,%rax
	movq %rax,%rbx
	movq $42,%rax
	cmpq %rax,%rbx
	je L.3
L.4:
	movq $512,%rax
	cmpq %rax,%rbx
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
