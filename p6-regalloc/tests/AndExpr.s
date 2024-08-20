	.text
AndExpr.And:
	pushq %rbp
	movq %rsp,%rbp
	movq %rsi,AndExpr.And.y
	movq %rdi,AndExpr.And.x
L.4:
	xorq t.0,t.0
	xorq %rax,%rax
	cmpq %rax,AndExpr.And.x
	je L.2
L.3:
	xorq %rax,%rax
	cmpq %rax,AndExpr.And.y
	je L.2
L.1:
	movq $1,t.0
L.2:
	movq t.0,%rax
L.0:
#	returnSink
	popq %rbp
	ret
AndExpr.And.badSub:
	call badSub
	.text
AndExpr:
	pushq %rbp
	movq %rsp,%rbp
L.5:
#	returnSink
	popq %rbp
	ret
