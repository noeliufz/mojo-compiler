	.text
MultiAnd.MultiAnd:
	pushq %rbp
	movq %rsp,%rbp
	movq %rdx,MultiAnd.MultiAnd.z
	movq %rsi,MultiAnd.MultiAnd.y
	movq %rdi,MultiAnd.MultiAnd.x
L.5:
	xorq %rax,%rax
	cmpq %rax,MultiAnd.MultiAnd.x
	je L.4
L.2:
	jmp L.0
L.4:
	movq $1,%rax
	cmpq %rax,MultiAnd.MultiAnd.y
	jne L.2
L.3:
	movq $2,%rax
	cmpq %rax,MultiAnd.MultiAnd.z
	jne L.2
L.1:
	jmp L.2
L.0:
#	returnSink
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
