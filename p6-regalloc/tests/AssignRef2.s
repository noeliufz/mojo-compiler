	.text
AssignRef2:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.0:
	movq $8,%rdi
	call malloc
	movq %rax,t.0
	movq $0,0(t.0)
	movq t.0,AssignRef2.1.x
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
