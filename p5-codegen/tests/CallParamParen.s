	.text
CallParamParen:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.0:
	movq $1,%rax
	movq %rax,%rdi
	call foo
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
CallParamParen.badSub:
	call badSub
