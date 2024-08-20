	.data
	.balign 8
ArrayIndexVariable.x:
	.zero 80
	.text
ArrayIndexVariable.get:
	pushq %rbp
	movq %rsp,%rbp
	movq %rdi,%rax
L.3:
	movq %rax,t.0
	xorq %rax,%rax
	cmpq %rax,t.0
	jl ArrayIndexVariable.get.badSub
L.1:
	movq $9,%rax
	cmpq %rax,t.0
	jg ArrayIndexVariable.get.badSub
L.2:
	leaq ArrayIndexVariable.x(%rip),%rax
	movq 0(%rax,t.0,8),t.4
	movq t.4,%rax
L.0:
#	returnSink
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
