	.text
AndBranch.And:
	pushq %rbp
	movq %rsp,%rbp
	movq %rsi,AndBranch.And.y
	movq %rdi,AndBranch.And.x
L.4:
	xorq %rax,%rax
	cmpq %rax,AndBranch.And.x
	je L.2
L.3:
	xorq %rax,%rax
	cmpq %rax,AndBranch.And.y
	je L.2
L.1:
	xorq %rax,%rax
	jmp L.0
L.2:
	movq $1,%rax
L.0:
#	returnSink
	popq %rbp
	ret
AndBranch.And.badSub:
	call badSub
	.text
AndBranch:
	pushq %rbp
	movq %rsp,%rbp
L.5:
#	returnSink
	popq %rbp
	ret
