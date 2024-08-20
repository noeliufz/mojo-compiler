	.data
	.balign 8
ArrayIndexVariable.x:
	.zero 80
	.text
ArrayIndexVariable.get:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rax
L.3:
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl ArrayIndexVariable.get.badSub
L.1:
	movq $9,%rax
	cmpq %rax,%rbx
	jg ArrayIndexVariable.get.badSub
L.2:
	leaq ArrayIndexVariable.x(%rip),%rax
#	movq %rbx,%rbx
	imulq $8,%rbx
#	movq %rax,%rax
	addq %rbx,%rax
	movq 0(%rax),%rax
#	movq %rax,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
ArrayIndexVariable.get.badSub:
	call badSub
	.text
ArrayIndexVariable:
	pushq %rbp
	movq %rsp,%rbp
L.4:
#	returnSink
	popq %rbp
	ret
