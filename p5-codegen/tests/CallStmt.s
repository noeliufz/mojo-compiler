	.text
CallStmt:
	pushq %rbp
	movq %rsp,%rbp
L.0:
	call f
#	movq %rax,%rax
#	returnSink
	popq %rbp
	ret
