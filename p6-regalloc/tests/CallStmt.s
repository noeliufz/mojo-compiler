	.text
CallStmt:
	pushq %rbp
	movq %rsp,%rbp
L.0:
	call f
	movq %rax,t.0
#	returnSink
	popq %rbp
	ret
