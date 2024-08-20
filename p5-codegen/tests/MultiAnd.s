	.text
MultiAnd.MultiAnd:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
#	movq %rdx,%rdx
	movq %rsi,%rcx
	movq %rdi,%rbx
L.5:
	xorq %rax,%rax
	cmpq %rax,%rbx
	je L.4
L.2:
	jmp L.0
L.4:
	movq $1,%rax
	cmpq %rax,%rcx
	jne L.2
L.3:
	movq $2,%rax
	cmpq %rax,%rdx
	jne L.2
L.1:
	jmp L.2
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
	.text
MultiAnd:
	pushq %rbp
	movq %rsp,%rbp
L.6:
#	returnSink
	popq %rbp
	ret
