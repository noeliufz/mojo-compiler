	.data
	.balign 8
SubrangeFields.r:
	.zero 8
	.text
SubrangeFields.println:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rbx
L.11:
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl L.1
L.2:
	xorq %rax,%rax
	cmpq %rax,%rbx
	je L.5
L.6:
	movq %rbp,%r10
	movq %rbx,%rdi
	call SubrangeFields.println.1.f
	movq %rax,t.3
L.7:
	movq $10,%rdi
	call putchar
	movq %rax,t.4
	jmp L.0
L.1:
	movq $45,%rdi
	call putchar
	movq %rax,t.1
	xorq %rax,%rax
	movq %rax,t.8
	subq %rbx,t.8
	movq t.8,%rbx
	jmp L.2
L.5:
	movq $48,%rdi
	call putchar
	movq %rax,t.2
	jmp L.7
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
SubrangeFields.println.badSub:
	call badSub
