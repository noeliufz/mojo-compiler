	.text
OrBranch.Or:
	pushq %rbp
	movq %rsp,%rbp
	movq %rsi,OrBranch.Or.y
	movq %rdi,OrBranch.Or.x
L.4:
	xorq %rax,%rax
	cmpq %rax,OrBranch.Or.x
	je L.3
L.1:
	xorq %rax,%rax
	jmp L.0
L.3:
	xorq %rax,%rax
	cmpq %rax,OrBranch.Or.y
	jne L.1
L.2:
	movq $1,%rax
L.0:
#	returnSink
	popq %rbp
	ret
OrBranch.Or.badSub:
	call badSub
	.text
OrBranch:
	pushq %rbp
	movq %rsp,%rbp
L.5:
#	returnSink
	popq %rbp
	ret
