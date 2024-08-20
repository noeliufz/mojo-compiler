	.text
FastFib.read_number:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.24:
	xorq %rbx,%rbx
L.1:
L.2:
	call getchar
#	movq %rax,%rax
	movq $10,%rcx
	cmpq %rcx,%rax
	je L.4
L.7:
	movq $13,%rcx
	cmpq %rcx,%rax
	je L.4
L.5:
	movq %rbx,t.6
	imulq $10,t.6
	movq %rax,t.7
	subq $48,t.7
	movq t.6,t.8
	addq t.7,t.8
	movq t.8,%rbx
L.6:
	jmp L.1
L.4:
L.3:
	movq %rbx,%rax
	jmp L.0
L.25:
	jmp L.6
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
FastFib.read_number.badSub:
	call badSub
