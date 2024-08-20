	.text
NotExpr.Not:
	pushq %rbp
	movq %rsp,%rbp
	movq %rdi,NotExpr.Not.x
L.3:
	xorq t.0,t.0
	xorq %rax,%rax
	cmpq %rax,NotExpr.Not.x
	je L.1
L.2:
	movq t.0,%rax
	jmp L.0
L.1:
	movq $1,t.0
	jmp L.2
L.0:
#	returnSink
	popq %rbp
	ret
NotExpr.Not.badSub:
	call badSub
	.text
NotExpr:
	pushq %rbp
	movq %rsp,%rbp
L.4:
#	returnSink
	popq %rbp
	ret
