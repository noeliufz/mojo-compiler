	.text
ReturnRecord.foo:
	pushq %rbp
	movq %rsp,%rbp
	subq $64,%rsp
	movq %rbx, -32(%rbp)
	movq %rdi,16(%rbp)
L.1:
	movq $0,-24(%rbp)
	movq $0,-16(%rbp)
	movq $0,-8(%rbp)
	movq $1,-24(%rbp)
	movq $2,-16(%rbp)
	movq $3,-8(%rbp)
	movq 16(%rbp),%rbx
	movq %rbx,t.0
	movq t.0,%rdi
	movq %rbp,%rax
	addq $-24,%rax
	movq %rax,%rsi
	movq $24,%rdx
	call memcpy
	movq %rax,t.1
	movq t.0,%rax
L.0:
#	returnSink
	movq -32(%rbp),%rbx
	addq $64,%rsp
	popq %rbp
	ret
ReturnRecord.foo.badSub:
	call badSub
